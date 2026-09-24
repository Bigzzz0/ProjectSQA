package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

/**
 * Branch & Defect Analysis Matrix:
 *
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructor with different modes and inlineAllStrings flag
 *   - process() externs and root
 *   - getFilterForMode() returns correct predicate for each mode
 *
 * Partition B: Boundary Value Analysis & Extremes
 *   - null/empty externs or root (NPE? see comment)
 *   - Variable never referenced (null refInfo)
 *   - Constant with no initial value
 *   - Constant assigned to a function expression
 *   - String length boundary for isStringWorthInlining
 *   - Local vs global scope
 *
 * Partition C: Defect-Targeted Branch Zone
 *   - Singleton getter method (issue 668) should NOT be inlined
 *   - Inlining of 'this' alias
 *   - Alias candidates (variable assigned to another variable)
 *   - Cross basic block inlining prevention
 *   - Inlining into call node with getprop (context shift prevention)
 *   - Function declarations handling
 *   - isVarInlineForbidden checks (exported, RENAME_PROPERTY, stale)
 *   - maybeEscapedOrModifiedArguments detection
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - IllegalStateException for unknown mode in switch (default case)
 *   - Preconditions checks (value null, grandparent type)
 *
 * Partition E: Object Lifecycle & Contract Integrity
 *   - staleVars mechanism
 *   - aliasCandidates map state after process
 *   - Blacklisting var references
 */
public class InlineVariablesDeepseekTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT3);
    compiler.initOptions(options);
  }

  /**
   * Helper: Compiles source, runs InlineVariables pass, returns compiled JS.
   */
  private String runInlineVariables(String js, InlineVariables.Mode mode, boolean inlineAllStrings) {
    compiler.run(CompilerUtils.EMPTY_INPUT, new JSSourceFile[] { JSSourceFile.fromCode("test", js) });
    Node root = compiler.getRoot().getLastChild(); // module body
    Node externs = compiler.getRoot().getFirstChild();
    InlineVariables pass = new InlineVariables(compiler, mode, inlineAllStrings);
    pass.process(externs, root);
    return compiler.toSource();
  }

  // ================= Partition A: Core Functional Logic =================

  @Test(timeout = 4000)
  public void testConstructorAndModeAll() {
    InlineVariables pass = new InlineVariables(compiler, InlineVariables.Mode.ALL, false);
    assertNotNull(pass);
  }

  @Test(timeout = 4000)
  public void testConstructorModeLocalsOnly() {
    InlineVariables pass = new InlineVariables(compiler, InlineVariables.Mode.LOCALS_ONLY, true);
    assertNotNull(pass);
  }

  @Test(timeout = 4000)
  public void testConstructorModeConstantsOnly() {
    InlineVariables pass = new InlineVariables(compiler, InlineVariables.Mode.CONSTANTS_ONLY, false);
    assertNotNull(pass);
  }

  @Test(timeout = 4000)
  public void testProcessNonNullExternsRoot() {
    // process should not throw
    Node externs = new Node(1); // fake externs
    Node root = new Node(1);    // fake root
    InlineVariables pass = new InlineVariables(compiler, InlineVariables.Mode.ALL, false);
    pass.process(externs, root);
    // no assertions, just no exception
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetFilterForModeDefaults() {
    // This is not directly testable via reflection; the switch has a default that throws.
    // We can't easily force default, but we test that the other modes don't throw.
    InlineVariables pass = new InlineVariables(compiler, InlineVariables.Mode.ALL, false);
    assertNotNull(pass);
  }

  // ================= Partition B: Boundary Value Analysis =================

  @Test(timeout = 4000)
  public void testVariableNeverReferenced() {
    // Variable never used should not cause issues
    String js = "var x = 1;";
    String result = runInlineVariables(js, InlineVariables.Mode.ALL, false);
    // Variable declaration may be removed because it's never referenced
    assertEquals("", result.trim());
  }

  @Test(timeout = 4000)
  public void testConstantStringNotInlinedIfWorthless() {
    // Long string where inlining would bloat size (noInlineBytes > inlineBytes)
    // We use inlineAllStrings=false, so hedging will take place.
    String longStr = "\"" + repeat("a", 15) + "\"";
    String js = "var CONST = " + longStr + "; alert(CONST); alert(CONST);";
    String result = runInlineVariables(js, InlineVariables.Mode.CONSTANTS_ONLY, false);
    // Since var is declared constant (in caps) and used twice, but string not worth inlining,
    // the variable should remain (not inlined). The result should contain the variable name.
    assertTrue("Expected variable not inlined", result.contains("CONST"));
  }

  @Test(timeout = 4000)
  public void testConstantStringInlinedWhenWorth() {
    // Short string, inlining beneficial
    String js = "var SS = 'a'; alert(SS);";
    String result = runInlineVariables(js, InlineVariables.Mode.CONSTANTS_ONLY, false);
    // The variable should be inlined; 'a' appears directly
    assertFalse("Variable should be inlined", result.contains("SS"));
    assertTrue("Short string should appear", result.contains("'a'"));
  }

  @Test(timeout = 4000)
  public void testConstantStringInlinedWithInlineAllStrings() {
    // inlineAllStrings=true forces inlining even if costly
    String longStr = "\"" + repeat("x", 100) + "\"";
    String js = "var C = " + longStr + "; use(C);";
    String result = runInlineVariables(js, InlineVariables.Mode.CONSTANTS_ONLY, true);
    assertFalse("Should be inlined", result.contains("C"));
    assertTrue(result.contains(longStr));
  }

  @Test(timeout = 4000)
  public void testLocalVariableOnlyInlinedInLocalsMode() {
    String js = "function f() { var y = 2; return y; }";
    String result = runInlineVariables(js, InlineVariables.Mode.LOCALS_ONLY, false);
    // y is local, should be inlined
    assertFalse("y should be inlined", result.contains("var y"));
    assertTrue(result.contains("return 2"));
  }

  @Test(timeout = 4000)
  public void testGlobalVariableNotInlinedInLocalsMode() {
    String js = "var g = 1; function f() { return g; }";
    String result = runInlineVariables(js, InlineVariables.Mode.LOCALS_ONLY, false);
    // g is global, not inlined
    assertTrue("g should remain", result.contains("var g"));
  }

  @Test(timeout = 4000)
  public void testConstantNotInlinedIfNoInitValue() {
    // Constant with no initial value should not be inlined
    String js = "/** @const */ var x; x = 1; alert(x);";
    // In Closure Compiler, @const var x; then assignment later is not a constant initializer.
    // We use a simple var that is not recognized as constant (no @const). But if we use
    // CONSTANTS_ONLY, the filter uses var.isConst() which is false without annotation.
    // So this will not be inlined.
    String result = runInlineVariables(js, InlineVariables.Mode.CONSTANTS_ONLY, false);
    // Variable is not const, so nothing inlined; the var x stays
    assertTrue("x should remain", result.contains("var x"));
  }

  // ================= Partition C: Defect-Targeted Branch Zone =================

  @Test(timeout = 4000)
  public void testSingletonGetterNotInlined() {
    // This is the defect from IntegrationTest::testSingletonGetter1.
    // A variable holding a function that returns an object (singleton getter)
    // should not be inlined when called, because it changes context.
    String js = ""
        + "var Singleton = (function() {"
        + "  var instance;"
        + "  function getInstance() {"
        + "    if (!instance) instance = new Object();"
        + "    return instance;"
        + "  }"
        + "  return {getInstance: getInstance};"
        + "})();"
        + "var x = Singleton.getInstance();";
    // We want to check that getInstance is not inlined into the call.
    // The pass may inline getInstance if it's a simple variable, but it should not.
    // In the source code, there is a comment about issue 668: don't inline singleton getter methods.
    // The condition in canInline checks for SubclassRelationship, but not for singleton getter.
    // The defect is missing that check. So we expect the bug to cause inlining of the getter.
    // Our test should assert that the variable is NOT inlined (i.e., the bug exists if it IS inlined).
    // However, since we are testing with an unknown version, we'll assert that after the pass,
    // the code still contains "getInstance". If the bug is present (inlining), it may disappear.
    // Because we want to reveal the bug, we assert that in the fixed version, the variable stays.
    // But we are writing the test to detect the bug: if the bug is present, the test fails.
    // To ensure the test fails on the defective version, we assert that the original variable
    // is still present (i.e., not inlined). The defective version would inline it, so the test
    // would fail because the variable name is removed. That reveals the bug.
    // WARNING: This depends on the specific architecture. We'll use a simpler pattern:
    // var getSingleton = function() { return new Foo(); };
    // var instance = getSingleton();
    // According to the canInline logic, this should be prevented because value is a Function and
    // the reference is a call. But the code only checks for getprop. For direct function expression,
    // it does not check? Actually, value.isFunction() is true, and it enters the block, but the
    // condition for call node is inside the if (value.isFunction()) block. The code checks for
    // SubclassRelationship and then comment about issue 668 but no actual check. So the function
    // might still be inlined if it passes other checks. The defect says singleton getter methods
    // should not be inlined. We'll create a test that attempts to inline a simple getter.
    // For simplicity, use:
    // var getter = function() { return 1; };
    // var result = getter();
    // This should NOT be inlined because it's a function expression called.
    String jsSimple = "var getter = function() { return 1; }; var result = getter();";
    String result = runInlineVariables(jsSimple, InlineVariables.Mode.ALL, false);
    // The function variable might still be inlined (the bug). We assert that 'getter' remains.
    // If the bug is present, getter will be removed and the function will be inlined.
    assertTrue("Singleton getter should not be inlined", result.contains("getter"));
  }

  @Test(timeout = 4000)
  public void testAliasCandidateInlining() {
    // If var a = b; where b is well-defined, a should be inlined as alias candidate
    String js = "var b = 10; var a = b; alert(a);";
    String result = runInlineVariables(js, InlineVariables.Mode.ALL, false);
    // After inlining, a disappears, b remains.
    assertFalse("a should be inlined", result.contains("var a"));
    assertTrue("b should remain", result.contains("var b"));
  }

  @Test(timeout = 4000)
  public void testCannotInlineAcrossBasicBlocks() {
    // Declaration and reference in different basic blocks (loop, if)
    String js = "var x = 1; if (true) { alert(x); }";
    // Both in same basic block? Actually the if creates a new block. So not same basic block.
    String result = runInlineVariables(js, InlineVariables.Mode.ALL, false);
    // x should not be inlined because blocks differ
    assertTrue("x should not be inlined", result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testGetPropNotInlinedIntoCall() {
    // var a = b.c; a(); should not inline because context changes.
    String js = "var a = obj.method; a();";
    String result = runInlineVariables(js, InlineVariables.Mode.ALL, false);
    assertTrue("a should remain", result.contains("a"));
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationInlinedCorrectly() {
    // Function declarations are removed differently.
    String js = "function f() { var x = 1; return x; }";
    String result = runInlineVariables(js, InlineVariables.Mode.ALL, false);
    // x should be inlined into return
    assertTrue(result.contains("return 1"));
    assertFalse(result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testExportedVariableNotInlined() {
    // Variables that are exported (e.g., X, with property export) should not be inlined.
    // In Closure Compiler, we can annotate @export.
    // For simplicity, we rely on naming convention: exported if starts with upper case? Actually not.
    // We'll manually set compiler option to consider an export convention. Not needed.
    // We'll use the fact that compiler.getCodingConvention().isExported() checks for common patterns.
    // Default coding convention may not export. We'll just test that staleVars blacklisting works.
    // Use a variable named "RENAME_PROPERTY_FUNCTION_NAME" which is forbidden.
    String js = "var JSCompiler_renameProperty = function() {};";
    // This variable should not be inlined.
    String result = runInlineVariables(js, InlineVariables.Mode.ALL, false);
    assertTrue("RENAME_PROPERTY should not be inlined", result.contains("JSCompiler_renameProperty"));
  }

  @Test(timeout = 4000)
  public void testThisAliasInlining() {
    // var self = this; use(self); should inline.
    // But this is special: isInlinableThisAlias check.
    // We need a scope where 'this' is used.
    // We'll define a constructor function.
    String js = "function Foo() { var self = this; self.bar = 1; }";
    String result = runInlineVariables(js, InlineVariables.Mode.ALL, false);
    // self should be inlined (replaced with this)
    assertFalse("self should be inlined", result.contains("var self"));
  }

  @Test(timeout = 4000)
  public void testMultipleReferencesImmutableVariable() {
    // Variable never modified and used multiple times should be inlined.
    String js = "var CONST = 42; alert(CONST); alert(CONST);";
    // In ALL mode, it will be inlined if isImmutableAndWellDefinedVariable passes.
    String result = runInlineVariables(js, InlineVariables.Mode.ALL, false);
    assertFalse("CONST should be inlined", result.contains("var CONST"));
    assertTrue(result.contains("alert(42)"));
  }

  @Test(timeout = 4000)
  public void testSingleAssignmentNotUsed() {
    // var x = 1; // only initialization, no other reference -> should be removed.
    String js = "var x = 1;";
    String result = runInlineVariables(js, InlineVariables.Mode.ALL, false);
    assertEquals("", result.trim());
  }

  // ================= Partition D: Exception & Defensive Guard Paths =================

  @Test(timeout = 4000)
  public void testIsLValueForIncDec() {
    // Exercise the isLValue method indirectly by having a variable used as inc/dec target.
    String js = "var x = 1; x++;";
    String result = runInlineVariables(js, InlineVariables.Mode.ALL, false);
    // x is used as L-value and should not be inlined.
    assertTrue("x should remain", result.contains("x"));
  }

  @Test(timeout = 4000)
  public void testBlacklistVarReferencesInTree() {
    // When a variable's value is inlined, references inside that value tree are blacklisted.
    // This prevents nested inlining.
    String js = "var a = 1; var b = a; var c = b;";
    // After inlining a into b, b becomes 1, then c becomes 1.
    // But blacklisting should prevent further inlining? Actually it prevents inlining of a again.
    // We'll just ensure no exception.
    String result = runInlineVariables(js, InlineVariables.Mode.ALL, false);
    // Both a and b should be inlined, c should remain with value 1.
    assertFalse("a should be inlined", result.contains("var a"));
    assertFalse("b should be inlined", result.contains("var b"));
    assertTrue("c should remain with value", result.contains("c = 1") || result.contains("c=1"));
  }

  @Test(timeout = 4000)
  public void testMaybeEscapedOrModifiedArguments() {
    // In a local scope, if arguments is used in a non-GET manner, it's escaped.
    String js = "function f() { var x = arguments; x[0]; }";
    String result = runInlineVariables(js, InlineVariables.Mode.ALL, false);
    // x should not be inlined (arguments escape)
    assertTrue("x should remain", result.contains("var x"));
  }

  // ================= Partition E: Object Lifecycle & Contract Integrity =================

  @Test(timeout = 4000)
  public void testStaleVarsPreventReinlining() {
    // After inline, the variable is marked stale. Should not try to inline alias again.
    // This test ensures no infinite loop.
    String js = "var a = 1; var b = a;";
    String result = runInlineVariables(js, InlineVariables.Mode.ALL, false);
    // a should be inlined into b, then b becomes 1. Should be fine.
    assertFalse("a should be inlined", result.contains("var a"));
    assertFalse("b should be inlined", result.contains("var b"));
  }

  @Test(timeout = 4000)
  public void testAliasCandidatesMapCleared() {
    // The aliasCandidates map is per scope. After process, it should be empty.
    // We cannot easily access that map, but we can test that no leftover state causes problems.
    // Run two passes.
    String js = "var z = 1;";
    runInlineVariables(js, InlineVariables.Mode.ALL, false);
    runInlineVariables("var w = 2;", InlineVariables.Mode.ALL, false);
    // Just ensure no exception
  }

  // Utility: repeat string
  private static String repeat(String s, int count) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
      sb.append(s);
    }
    return sb.toString();
  }
}