package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit 4 test suite for {@link RemoveUnusedVars}.
 * Targets maximum line and branch coverage, including the known defect
 * where unused function parameters are removed even when {@code removeGlobals} is false.
 */
public class RemoveUnusedVarsDeepseekTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    // Typical advanced optimizations settings; we override some flags per test.
    options.setIdeMode(false);
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT3);
    options.setLanguageOut(CompilerOptions.LanguageMode.ECMASCRIPT3);
  }

  /**
   * Helper: compile source with given RemoveUnusedVars configuration and return the generated source.
   */
  private String compileAndRemoveUnused(String code,
                                         boolean removeGlobals,
                                         boolean preserveFunctionExpressionNames,
                                         boolean modifyCallSites) {
    compiler.init(
        new JSSourceFile[] { JSSourceFile.fromCode("externs", "") },
        new JSSourceFile[] { JSSourceFile.fromCode("test", code) },
        options);
    // The compiler must be normalized before running RemoveUnusedVars.
    compiler.getLifeCycleStage().setNormalized();
    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, removeGlobals,
        preserveFunctionExpressionNames, modifyCallSites);
    pass.process(compiler.getExternsRoot(), compiler.getJsRoot());
    return compiler.toSource();
  }

  // ================================
  // Tests targeting the known defect (Issue 168b)
  // ================================

  /**
   * @target removeUnreferencedFunctionArgs
   * @scenario removeGlobals = false, function with unused parameter y.
   * @defectRisk Bug: parameter y should be kept because removeGlobals=false,
   *             but it is erroneously removed.
   *             Fixed version preserves y.
   */
  @Test(timeout = 4000)
  public void testIssue168b_DoNotRemoveParamsWhenNotRemovingGlobals() {
    String code = "function a(x, y) { return x; }";
    String result = compileAndRemoveUnused(code, false, false, false);
    // Expected: the function signature is unchanged because y is a global var.
    assertTrue("Parameter y should not be removed when removeGlobals=false",
        result.contains("function a(x, y)"));
    // Also ensure x is still there.
    assertTrue(result.contains("function a(x, y) { return x; }") ||
               result.contains("function a(x, y) {\n  return x;\n}"));
  }

  // ================================
  // Basic variable removal tests
  // ================================

  /**
   * @target removeUnreferencedVars
   * @scenario Unreferenced var declaration with no side effects.
   * @defectRisk Should be removed.
   */
  @Test(timeout = 4000)
  public void testRemoveUnreferencedVar() {
    String code = "var x = 1; var y = 2; var z = x;";
    // With removeGlobals = true, only z is used (via x). x and y removed? x is used, y unused.
    // Actually z uses x, so x stays, y removed.
    String result = compileAndRemoveUnused(code, true, false, false);
    assertTrue("Remaining code should contain 'var x' or x assignment",
        result.contains("var x = 1") || result.contains("x = 1"));
    assertTrue("Remaining code should contain 'var z' or z assignment",
        result.contains("var z") || result.contains("z = x"));
  }

  /**
   * @target isRemovableVar, markReferencedVar
   * @scenario Exported variable should not be removed.
   * @defectRisk Exported variable could be erroneously removed.
   */
  @Test(timeout = 4000)
  public void testExportedVarNotRemoved() {
    String code = "var x = 1; window['x'] = x;"; // x is exported via window['x']
    String result = compileAndRemoveUnused(code, true, false, false);
    // x is used, so it remains.
    assertTrue(result.contains("var x = 1") || result.contains("x = 1"));
  }

  // ================================
  // Property assignment tests
  // ================================

  /**
   * @target Assign, interpretAssigns, isPropertyAssign
   * @scenario Variable with only property assignments, not otherwise referenced,
   *          and assigned to literal -> should be removed.
   * @defectRisk Property assignments may be mistakenly counted as references.
   */
  @Test(timeout = 4000)
  public void testPropertyAssignOnLiteralObject() {
    String code = "var x = {}; x.foo = 3;";
    String result = compileAndRemoveUnused(code, true, false, false);
    // x is never read, so it should be removed entirely.
    assertTrue("x should be removed",
        !result.contains("x"));
  }

  /**
   * @target Assign with side effects
   * @scenario Variable assigned with side‑effectful expression – should be kept.
   * @defectRisk Could incorrectly remove if side effects ignored.
   */
  @Test(timeout = 4000)
  public void testSideEffectfulAssign() {
    String code = "var x = alert(1); alert(x);"; // x is referenced
    String result = compileAndRemoveUnused(code, true, false, false);
    assertTrue(result.contains("alert(1)"));
  }

  // ================================
  // Continuation tests (lazy traversal)
  // ================================

  /**
   * @target Continuation, markReferencedVar
   * @scenario Variable declaration with unused function expression.
   * @defectRisk Function may not be visited until the variable is referenced.
   */
  @Test(timeout = 4000)
  public void testContinuationWithFunctionDeclaration() {
    String code = "var a = function() { var b = 1; };";
    String result = compileAndRemoveUnused(code, true, false, false);
    // a is unused, should be removed; b is inside a and unreferenced.
    assertTrue("Code should be empty or have no declarations",
        !result.contains("a") && !result.contains("b"));
  }

  /**
   * @target Continuation with assignment
   * @scenario Assign to variable that is later referenced – the continuation should execute.
   * @defectRisk Assign may be removed prematurely.
   */
  @Test(timeout = 4000)
  public void testAssignContinuationLaterReferenced() {
    String code = "var x; x = 1; var y = x;";
    String result = compileAndRemoveUnused(code, true, false, false);
    // x is referenced by y, so both should remain.
    assertTrue(result.contains("x = 1") || result.contains("var x = 1"));
    assertTrue(result.contains("y = x") || result.contains("var y = x"));
  }

  // ================================
  // modifiyCallSites tests
  // ================================

  /**
   * @target CallSiteOptimizer, removeUnreferencedFunctionArgs with modifyCallSites=true
   * @scenario Unused parameter should be removed from function def and calls.
   * @defectRisk Must not break when there are side effects.
   */
  @Test(timeout = 4000)
  public void testModifyCallSitesRemoveUnusedArg() {
    String code = "function f(a, b) { return a; }; f(1, 2);";
    String result = compileAndRemoveUnused(code, true, false, true);
    // Should become function f(a) { return a; }; f(1);
    assertTrue("b parameter removed", !result.contains(", b"));
    assertTrue("call with only one arg", result.contains("f(1)"));
  }

  /**
   * @target CallSiteOptimizer with side effects in unused arg
   * @scenario Unused arg has side effects – must not be removed.
   * @defectRisk Could remove a side‑effecting argument.
   */
  @Test(timeout = 4000)
  public void testModifyCallSitesSideEffectUnusedArg() {
    String code = "function f(a, b) { return a; }; f(1, alert(2));";
    String result = compileAndRemoveUnused(code, true, false, true);
    // The argument alert(2) has side effects, so must stay.
    assertTrue(result.contains("alert(2)"));
  }

  // ================================
  // preserveFunctionExpressionNames
  // ================================

  /**
   * @target preserveFunctionExpressionNames
   * @scenario Unused named function expression, names should be preserved.
   * @defectRisk Name may be stripped even when preserve=true.
   */
  @Test(timeout = 4000)
  public void testPreserveFunctionExpressionNames() {
    String code = "var x = function myName() {};";
    String result = compileAndRemoveUnused(code, true, true, false);
    // x is unused, but function expression name should remain.
    assertTrue("Function expression name preserved", result.contains("myName"));
  }

  /**
   * @target preserveFunctionExpressionNames=false
   * @scenario Unused named function expression, name should be removed.
   */
  @Test(timeout = 4000)
  public void testRemoveFunctionExpressionNames() {
    String code = "var x = function myName() {};";
    String result = compileAndRemoveUnused(code, true, false, false);
    // x is unused, so the entire var statement should be removed.
    // But if x is removed, the name is irrelevant.
    // Actually x is unused, so whole thing removed. So no myName.
    assertTrue("Code should be empty", result.trim().isEmpty());
  }

  // ================================
  // Inheritance calls (goog.inherits)
  // ================================

  /**
   * @target classDefiningCalls
   * @scenario Unused subclass via goog.inherits should be removed.
   * @defectRisk Inheritance call may be kept.
   */
  @Test(timeout = 4000)
  public void testRemoveUnusedInheritsCall() {
    String code = "function Parent() {}; function Child() {}; goog.inherits(Child, Parent);";
    // If Child is never used, the inherits call should be removed.
    String result = compileAndRemoveUnused(code, true, false, false);
    assertTrue("goog.inherits should be removed", !result.contains("goog.inherits"));
  }

  /**
   * @target classDefiningCalls, subclass referenced
   * @scenario Subclass used -> inherits call must remain.
   */
  @Test(timeout = 4000)
  public void testKeepInheritsWhenSubclassUsed() {
    String code = "function Parent() {}; function Child() {}; goog.inherits(Child, Parent); new Child();";
    String result = compileAndRemoveUnused(code, true, false, false);
    assertTrue("goog.inherits kept", result.contains("goog.inherits"));
  }

  // ================================
  // Edge cases: for-in, catch, arguments
  // ================================

  /**
   * @target removeUnreferencedVars for-in variable
   * @scenario for-in variable should not be removed.
   * @defectRisk Could remove the loop variable.
   */
  @Test(timeout = 4000)
  public void testForInVariableNotRemoved() {
    String code = "var x = {a:1}; for (var k in x) { alert(k); }";
    String result = compileAndRemoveUnused(code, true, false, false);
    assertTrue("k should not be removed", result.contains("k"));
  }

  /**
   * @target escape of 'arguments'
   * @scenario If 'arguments' is used, all function parameters must be referenced.
   * @defectRisk Parameters may be incorrectly removed.
   */
  @Test(timeout = 4000)
  public void testArgumentsEscaped() {
    String code = "function f(a, b) { return arguments[0]; }";
    String result = compileAndRemoveUnused(code, true, false, false);
    // both parameters are considered referenced because 'arguments' is used.
    assertTrue("Parameter a remains", result.contains("a"));
    assertTrue("Parameter b remains", result.contains("b"));
  }

  // ================================
  // Multiple var declarations
  // ================================

  /**
   * @target removeUnreferencedVars: multi-var
   * @scenario var declaration with multiple names, only some unreferenced.
   * @defectRisk May remove entire var instead of individual names.
   */
  @Test(timeout = 4000)
  public void testRemoveSingleNameFromMultiVar() {
    String code = "var a = 1, b = 2; var c = a;";
    String result = compileAndRemoveUnused(code, true, false, false);
    // b is unused, should be removed. a and c remain.
    assertTrue(result.contains("a = 1") || result.contains("var a = 1"));
    assertTrue("b should be removed", !result.contains("b"));
  }

  // ================================
  // Null / empty / no side effects
  // ================================

  /**
   * @target traverseNode with empty block
   * @scenario Empty function body.
   * @defectRisk Should not crash.
   */
  @Test(timeout = 4000)
  public void testEmptyFunction() {
    String code = "function f() {}";
    String result = compileAndRemoveUnused(code, true, false, false);
    assertTrue("f remains if referenced? It is not referenced -> should be removed",
        !result.contains("f") || result.trim().isEmpty());
  }

  /**
   * @target literal values in var
   * @scenario Unreferenced var with literal initializer.
   */
  @Test(timeout = 4000)
  public void testUnreferencedLiteralVar() {
    String code = "var x = 42;";
    String result = compileAndRemoveUnused(code, true, false, false);
    assertTrue("x should be removed", !result.contains("x"));
  }

  // ================================
  // Side‑effectful initializer in var should be kept when variable is removed
  // ================================

  /**
   * @target removeUnreferencedVars: side effect in var
   * @scenario var with side effect (e.g., alert) -> should convert to expression statement.
   * @defectRisk May incorrectly remove the side effect.
   */
  @Test(timeout = 4000)
  public void testSideEffectInVarInitializer() {
    String code = "var x = alert(1);";
    String result = compileAndRemoveUnused(code, true, false, false);
    // x is removed, but alert(1) must remain.
    assertTrue("alert should remain", result.contains("alert(1)"));
  }

  // ================================
  // Interplay: var with property assign and no reference
  // ================================

  /**
   * @target interpretAssigns, assignedToUnknownValue + property assign
   * @scenario var assigned to unknown value and property assigned -> var must be kept.
   * @defectRisk May be removed if escaping detection fails.
   */
  @Test(timeout = 4000)
  public void testUnknownValueWithPropertyAssign() {
    String code = "var x = foo(); x.y = 3;";
    // x is assigned to an unknown value (foo result) and then a property is assigned.
    // The property assign might escape x, so x must be kept.
    String result = compileAndRemoveUnused(code, true, false, false);
    // In practice, x is considered referenced via the property assign.
    assertTrue("x should be kept", result.contains("x"));
  }

  // ================================
  // preserveGlobal: when removeGlobals=false, global var should stay even if unused
  // ================================

  /**
   * @target isRemovableVar, removeGlobals=false
   * @scenario Unused global var when removeGlobals=false.
   * @defectRisk May be removed.
   */
  @Test(timeout = 4000)
  public void testGlobalVarKeptWhenNotRemovingGlobals() {
    String code = "var x = 1;";
    String result = compileAndRemoveUnused(code, false, false, false);
    assertTrue("x should be kept", result.contains("x"));
  }

  // ================================
  // modifyCallSites with canChangeSignature=false (varargs)
  // ================================

  /**
   * @target canModifyCallers for varargs function
   * @scenario Function with arguments (varargs) should not have signature changed.
   * @defectRisk May attempt to modify callers incorrectly.
   */
  @Test(timeout = 4000)
  public void testVarargsFunctionSignatureNotChanged() {
    String code = "function f() { return arguments; }; f(1, 2);";
    String result = compileAndRemoveUnused(code, true, false, true);
    // Because of arguments, it's considered varargs, so signature unchanged.
    assertTrue("Function should still have no params", result.contains("function f()"));
    assertTrue("Call should still have two args", result.contains("f(1, 2)"));
  }

  // ================================
  // Reaching fixed point via interpretAssigns
  // ================================

  /**
   * @target interpretAssigns fixed point
   * @scenario Chain: var a; a = 1; var b = a; -> a referenced, b unreferenced.
   * @defectRisk Loop might not converge.
   */
  @Test(timeout = 4000)
  public void testInterpretAssignsFixedPoint() {
    String code = "var a; a = 1; var b = a;";
    String result = compileAndRemoveUnused(code, true, false, false);
    assertTrue("a remains", result.contains("a"));
    assertTrue("b should be removed", !result.contains("b"));
  }

  // ================================
  // Node with multiple type handling (ASSIGN, CALL, NAME)
  // ================================

  /**
   * @target traverseNode for CALL (singletonGetter)
   * @scenario goog.addSingletonGetter call for unused class.
   * @defectRisk May keep the call or incorrectly mark var.
   */
  @Test(timeout = 4000)
  public void testSingletonGetterCall() {
    String code = "function MyClass() {}; goog.addSingletonGetter(MyClass);";
    String result = compileAndRemoveUnused(code, true, false, false);
    // If MyClass is never used, the getter call should be removed.
    assertTrue("addSingletonGetter should be removed", !result.contains("addSingletonGetter"));
  }
}