package com.google.javascript.jscomp;

import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target Class: com.google.javascript.jscomp.VarCheck
 * Defects4J Known Defect:
 * - createSynthesizedExternVar() creates a new VAR node in the synthetic externs
 *   root without calling compiler.reportCodeChange().
 * - When extern property/var references or function calls are detected,
 *   VarCheck must notify the compiler that code changes occurred.
 *
 * Decision / Condition Coverage Zones:
 * 1. Node type filter: n.getType() != Token.NAME (early exit) vs Token.NAME.
 * 2. Empty variable name:
 *    - NodeUtil.isFunction(parent) precondition.
 *    - NodeUtil.isFunctionExpression(parent) -> valid empty name.
 *    - !NodeUtil.isFunctionExpression(parent) -> report INVALID_FUNCTION_DECL.
 * 3. Pre-existing extern candidates (varsToDeclareInExterns.contains(varName)):
 *    - parent is VAR -> createSynthesizedExternVar + addSuppression("duplicate").
 *    - parent is FunctionDeclaration -> createSynthesizedExternVar + addSuppression("duplicate").
 * 4. Var resolution in scope (scope.getVar(varName)):
 *    - var == null:
 *        - NodeUtil.isFunctionExpression(parent) -> allowed undeclared name.
 *        - strictExternCheck && t.getInput().isExtern() -> suppression of second error.
 *        - !strictExternCheck || !t.getInput().isExtern() -> report UNDEFINED_VAR_ERROR.
 *        - sanityCheck == true -> throw IllegalStateException("Unexpected variable " + varName).
 *        - sanityCheck == false -> createSynthesizedExternVar + scope.declare.
 *    - var != null:
 *        - currInput == varInput || currInput == null || varInput == null -> valid same-file.
 *        - varModule != currModule:
 *            - moduleGraph.dependsOn(currModule, varModule) -> valid dependency.
 *            - !sanityCheck && scope.isGlobal():
 *                - moduleGraph.dependsOn(varModule, currModule) -> report VIOLATED_MODULE_DEP_ERROR.
 *                - no dependency relationship -> report MISSING_MODULE_DEP_ERROR.
 *            - sanityCheck || !scope.isGlobal() -> report STRICT_MODULE_DEP_ERROR.
 * 5. NameRefInExternsCheck (traversing externs AST):
 *    - parent is VAR, FUNCTION, LP -> valid in externs.
 *    - parent is GETPROP:
 *        - n == parent.getFirstChild() && var == null -> report UNDEFINED_EXTERN_VAR_ERROR + add to externs.
 *        - n == parent.getFirstChild() && var != null -> valid property root.
 *    - default (e.g., CALL, EXPR_RESULT):
 *        - report NAME_REFERENCE_IN_EXTERNS_ERROR.
 *        - var == null -> add to externs.
 * 6. Coding convention constant check:
 *    - compiler.getCodingConvention().isConstant(varName) -> set IS_CONSTANT_NAME.
 */
public class VarCheckGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & Normal Operations
  // =========================================================================

  @Test(timeout = 4000)
  public void testWellFormedCodeWithoutErrors() {
    Compiler compiler = runVarCheck(
        "var window;",
        "var a = window; var b = 1; var c = b + 1;",
        false);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testUndefinedVariableInMainCodeReportsError() {
    Compiler compiler = runVarCheck(
        "var window;",
        "x = 10;",
        false);
    assertEquals(1, compiler.getErrorCount());
    assertTrue("Should report UNDEFINED_VAR_ERROR",
        hasError(compiler, VarCheck.UNDEFINED_VAR_ERROR));
  }

  @Test(timeout = 4000)
  public void testAnonymousFunctionExpressionNameAllowed() {
    Compiler compiler = runVarCheck(
        "",
        "var f = function foo() { foo(); };",
        false);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testAnonymousFunctionWithoutNameAllowed() {
    Compiler compiler = runVarCheck(
        "",
        "var f = function() {};",
        false);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testDuplicateVarSuppressionWhenExternPending() {
    Compiler compiler = runVarCheck(
        "extObj.foo = 1;",
        "var extObj = { foo: 2 };",
        false);
    // UNDEFINED_EXTERN_VAR_ERROR warning for extObj in externs
    assertTrue(hasWarning(compiler, VarCheck.UNDEFINED_EXTERN_VAR_ERROR));
    // But in main code, var extObj is recognized and duplicate suppression applied
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationDuplicateSuppressionWhenExternPending() {
    Compiler compiler = runVarCheck(
        "extFn.bar = 1;",
        "function extFn() {}",
        false);
    assertTrue(hasWarning(compiler, VarCheck.UNDEFINED_EXTERN_VAR_ERROR));
    assertEquals(0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extern Checks
  // =========================================================================

  @Test(timeout = 4000)
  public void testExternAllowedPatterns() {
    Compiler compiler = runVarCheck(
        "var a; function f(param) {}",
        "var b = a;",
        false);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExternGetPropWithDeclaredRootDoesNotWarn() {
    Compiler compiler = runVarCheck(
        "var a; a.b = 1; a.b.c = 2;",
        "",
        false);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExternNameReferenceInDefaultContextWarns() {
    Compiler compiler = runVarCheck(
        "alert(1);",
        "",
        false);
    assertTrue("Should report NAME_REFERENCE_IN_EXTERNS_ERROR",
        hasWarning(compiler, VarCheck.NAME_REFERENCE_IN_EXTERNS_ERROR));
  }

  @Test(timeout = 4000)
  public void testExternNameReferenceWithPredeclaredVarWarns() {
    Compiler compiler = runVarCheck(
        "var alert; alert(1);",
        "",
        false);
    assertTrue("Should report NAME_REFERENCE_IN_EXTERNS_ERROR",
        hasWarning(compiler, VarCheck.NAME_REFERENCE_IN_EXTERNS_ERROR));
    // Since alert was declared, no pending extern is added
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testSynthesizedConstantExternVariable() {
    Compiler compiler = runVarCheck(
        "",
        "MY_GLOBAL_CONSTANT = 42;",
        false);
    assertTrue(hasError(compiler, VarCheck.UNDEFINED_VAR_ERROR));
    assertTrue(compiler.getCodingConvention().isConstant("MY_GLOBAL_CONSTANT"));
  }

  @Test(timeout = 4000)
  public void testStrictExternCheckSuppressesSecondError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setWarningLevel(
        new DiagnosticGroup(VarCheck.UNDEFINED_EXTERN_VAR_ERROR), CheckLevel.ERROR);

    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs.js", "missingExt.prop = 1;")),
        Collections.singletonList(SourceFile.fromCode("testcode.js", "")),
        options);
    Node root = compiler.parseInputs();
    Node externsNode = root.getFirstChild();
    Node mainNode = externsNode.getNext();

    VarCheck check = new VarCheck(compiler, false);
    check.process(externsNode, mainNode);

    // Should have reported UNDEFINED_EXTERN_VAR_ERROR as error
    assertTrue(hasError(compiler, VarCheck.UNDEFINED_EXTERN_VAR_ERROR));
    // But NOT UNDEFINED_VAR_ERROR
    assertFalse(hasError(compiler, VarCheck.UNDEFINED_VAR_ERROR));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J ground truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefectPropReferenceInExternsReportsCodeChange() {
    Compiler compiler = new Compiler();
    CodeChangeHandler.RecentChange recentChange = new CodeChangeHandler.RecentChange();
    compiler.addChangeHandler(recentChange);

    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs.js", "asdf.foo;")),
        Collections.singletonList(SourceFile.fromCode("testcode.js", "var asdf;")),
        options);
    Node root = compiler.parseInputs();
    Node externsNode = root.getFirstChild();
    Node mainNode = externsNode.getNext();

    VarCheck check = new VarCheck(compiler);
    check.process(externsNode, mainNode);

    // KNOWN DEFECT: createSynthesizedExternVar should trigger reportCodeChange()
    assertTrue("compiler.reportCodeChange() should have been called",
        recentChange.hasCodeChanged());
  }

  @Test(timeout = 4000)
  public void testDefectCallInExternsReportsCodeChange() {
    Compiler compiler = new Compiler();
    CodeChangeHandler.RecentChange recentChange = new CodeChangeHandler.RecentChange();
    compiler.addChangeHandler(recentChange);

    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs.js", "externalFunction();")),
        Collections.singletonList(SourceFile.fromCode("testcode.js", "")),
        options);
    Node root = compiler.parseInputs();
    Node externsNode = root.getFirstChild();
    Node mainNode = externsNode.getNext();

    VarCheck check = new VarCheck(compiler);
    check.process(externsNode, mainNode);

    // KNOWN DEFECT: externalFunction synthesized in externs must trigger reportCodeChange()
    assertTrue("compiler.reportCodeChange() should have been called",
        recentChange.hasCodeChanged());
  }

  @Test(timeout = 4000)
  public void testDefectUndeclaredVarInMainReportsCodeChange() {
    Compiler compiler = new Compiler();
    CodeChangeHandler.RecentChange recentChange = new CodeChangeHandler.RecentChange();
    compiler.addChangeHandler(recentChange);

    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs.js", "")),
        Collections.singletonList(SourceFile.fromCode("testcode.js", "undeclaredVar = 1;")),
        options);
    Node root = compiler.parseInputs();
    Node externsNode = root.getFirstChild();
    Node mainNode = externsNode.getNext();

    VarCheck check = new VarCheck(compiler);
    check.process(externsNode, mainNode);

    // KNOWN DEFECT: synthesized extern variable created in main code must report code change
    assertTrue("compiler.reportCodeChange() should have been called",
        recentChange.hasCodeChanged());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testSanityCheckModeThrowsOnUndefinedVariable() {
    runVarCheck("", "undeclared = 1;", true);
  }

  @Test(timeout = 4000)
  public void testSanityCheckModeSkipsExternCheck() {
    // In sanity check mode, NameRefInExternsCheck is skipped
    Compiler compiler = runVarCheck("undeclaredExtern.prop = 1;", "var x = 1;", true);
    assertFalse(hasWarning(compiler, VarCheck.UNDEFINED_EXTERN_VAR_ERROR));
  }

  @Test(timeout = 4000)
  public void testInvalidFunctionDeclarationWithEmptyName() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs.js", "")),
        Collections.singletonList(SourceFile.fromCode("testcode.js", "function valid() {}")),
        options);
    Node root = compiler.parseInputs();
    Node externsNode = root.getFirstChild();
    Node mainNode = externsNode.getNext();

    // Mutate the function declaration AST to have an empty name
    Node script = mainNode.getFirstChild();
    Node fn = script.getFirstChild();
    assertEquals(Token.FUNCTION, fn.getType());
    Node nameNode = fn.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    nameNode.setString("");

    VarCheck check = new VarCheck(compiler, false);
    check.process(externsNode, mainNode);

    assertTrue("Should report INVALID_FUNCTION_DECL",
        hasError(compiler, VarCheck.INVALID_FUNCTION_DECL));
  }

  // =========================================================================
  // Partition E: Module Dependency Boundary Zones
  // =========================================================================

  @Test(timeout = 4000)
  public void testValidModuleDependencySucceeds() {
    JSModule m1 = new JSModule("m1");
    m1.add(SourceFile.fromCode("m1.js", "var x = 1;"));

    JSModule m2 = new JSModule("m2");
    m2.add(SourceFile.fromCode("m2.js", "var y = x;"));
    m2.addDependency(m1);

    Compiler compiler = runModuleVarCheck(Lists.newArrayList(m1, m2), false);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testViolatedModuleDependencyReportsError() {
    JSModule m1 = new JSModule("m1");
    m1.add(SourceFile.fromCode("m1.js", "var x = 1;"));

    JSModule m2 = new JSModule("m2");
    m2.add(SourceFile.fromCode("m2.js", "var y = x;"));
    // m1 depends on m2 (so m2 loads first, but references x from m1 which loads after m2)
    m1.addDependency(m2);

    Compiler compiler = runModuleVarCheck(Lists.newArrayList(m2, m1), false);
    assertTrue("Should report VIOLATED_MODULE_DEP_ERROR",
        hasError(compiler, VarCheck.VIOLATED_MODULE_DEP_ERROR));
  }

  @Test(timeout = 4000)
  public void testMissingModuleDependencyReportsWarning() {
    JSModule m1 = new JSModule("m1");
    m1.add(SourceFile.fromCode("m1.js", "var x = 1;"));

    JSModule m2 = new JSModule("m2");
    m2.add(SourceFile.fromCode("m2.js", "var y = x;"));
    // No dependency relationship

    Compiler compiler = runModuleVarCheck(Lists.newArrayList(m1, m2), false);
    assertTrue("Should report MISSING_MODULE_DEP_ERROR",
        hasWarning(compiler, VarCheck.MISSING_MODULE_DEP_ERROR));
  }

  @Test(timeout = 4000)
  public void testStrictModuleDependencyInNonGlobalScope() {
    JSModule m1 = new JSModule("m1");
    m1.add(SourceFile.fromCode("m1.js", "var x = 1;"));

    JSModule m2 = new JSModule("m2");
    m2.add(SourceFile.fromCode("m2.js", "function f() { return x; }"));

    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setWarningLevel(
        new DiagnosticGroup(VarCheck.STRICT_MODULE_DEP_ERROR), CheckLevel.ERROR);

    compiler.initModules(
        Collections.singletonList(SourceFile.fromCode("externs.js", "")),
        Lists.newArrayList(m1, m2),
        options);
    Node root = compiler.parseInputs();
    Node externsNode = root.getFirstChild();
    Node mainNode = externsNode.getNext();

    VarCheck check = new VarCheck(compiler, false);
    check.process(externsNode, mainNode);

    assertTrue("Should report STRICT_MODULE_DEP_ERROR",
        hasError(compiler, VarCheck.STRICT_MODULE_DEP_ERROR));
  }

  @Test(timeout = 4000)
  public void testStrictModuleDependencyInSanityCheckMode() {
    JSModule m1 = new JSModule("m1");
    m1.add(SourceFile.fromCode("m1.js", "var x = 1;"));

    JSModule m2 = new JSModule("m2");
    m2.add(SourceFile.fromCode("m2.js", "var y = x;"));

    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setWarningLevel(
        new DiagnosticGroup(VarCheck.STRICT_MODULE_DEP_ERROR), CheckLevel.ERROR);

    compiler.initModules(
        Collections.singletonList(SourceFile.fromCode("externs.js", "")),
        Lists.newArrayList(m1, m2),
        options);
    Node root = compiler.parseInputs();
    Node externsNode = root.getFirstChild();
    Node mainNode = externsNode.getNext();

    VarCheck check = new VarCheck(compiler, true);
    check.process(externsNode, mainNode);

    assertTrue("Sanity check mode should trigger STRICT_MODULE_DEP_ERROR",
        hasError(compiler, VarCheck.STRICT_MODULE_DEP_ERROR));
  }

  @Test(timeout = 4000)
  public void testSameModuleCrossFileVariableReferenceAllowed() {
    JSModule m = new JSModule("m");
    m.add(SourceFile.fromCode("f1.js", "var x = 1;"));
    m.add(SourceFile.fromCode("f2.js", "var y = x;"));

    Compiler compiler = runModuleVarCheck(Collections.singletonList(m), false);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  // =========================================================================
  // Test Helpers
  // =========================================================================

  private Compiler runVarCheck(String externsCode, String jsCode, boolean sanityCheck) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs.js", externsCode)),
        Collections.singletonList(SourceFile.fromCode("testcode.js", jsCode)),
        options);
    Node root = compiler.parseInputs();
    assertNotNull("Parsed AST root should not be null", root);
    Node externsNode = root.getFirstChild();
    Node mainNode = externsNode.getNext();

    VarCheck check = new VarCheck(compiler, sanityCheck);
    check.process(externsNode, mainNode);
    return compiler;
  }

  private Compiler runModuleVarCheck(List<JSModule> modules, boolean sanityCheck) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initModules(
        Collections.singletonList(SourceFile.fromCode("externs.js", "")),
        modules,
        options);
    Node root = compiler.parseInputs();
    assertNotNull("Parsed AST root should not be null", root);
    Node externsNode = root.getFirstChild();
    Node mainNode = externsNode.getNext();

    VarCheck check = new VarCheck(compiler, sanityCheck);
    check.process(externsNode, mainNode);
    return compiler;
  }

  private boolean hasError(Compiler compiler, DiagnosticType type) {
    for (JSError error : compiler.getErrors()) {
      if (error.getType() == type) {
        return true;
      }
    }
    return false;
  }

  private boolean hasWarning(Compiler compiler, DiagnosticType type) {
    for (JSError warning : compiler.getWarnings()) {
      if (warning.getType() == type) {
        return true;
      }
    }
    return false;
  }
}