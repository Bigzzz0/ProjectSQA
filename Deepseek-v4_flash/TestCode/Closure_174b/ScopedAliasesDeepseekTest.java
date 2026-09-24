package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Comprehensive white-box test suite for {@link ScopedAliases}.
 * Targets all key decision branches, state transitions, and the known Defects4J bug
 * (testIssue1103a/b/c) where GOOG_SCOPE_NON_ALIAS_LOCAL is incorrectly reported.
 *
 * Branch & Defect Analysis Matrix:
 * - Partition A: Core alias processing and error-free goog.scope transformation.
 * - Partition B: Error diagnostics (improper use, bad params, this, return, throw, redefinition, cycle).
 * - Partition C: Non-alias local detection (more permissive than original?).
 * - Partition D: Type node alias handling.
 * - Partition E: Scope shadow renaming, hotSwapScript, and edge cases.
 */
@RunWith(JUnit4.class)
public class ScopedAliasesDeepseekTest {

  private static final String GOOG_SCOPE_CODE_PREFIX =
      "goog.scope(function() {\n";
  private static final String GOOG_SCOPE_CODE_SUFFIX =
      "});";

  private Compiler getCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    // Ensure the library is injected (needed for $jscomp.scope patterns)
    options.setIdeMode(true);
    options.setCodingConvention(new DefaultCodingConvention());
    compiler.init(
        Collections.<SourceFile>emptyList(),
        Lists.newArrayList(SourceFile.fromCode("testcode", "")),
        options);
    return compiler;
  }

  private Result compileAndGetResult(String js) {
    Compiler compiler = getCompiler();
    // Compile with goog.scope and enough optimization to trigger ScopedAliases
    CompilerOptions options = new CompilerOptions();
    options.setIdeMode(false); // Allow optimizations
    options.setCodingConvention(new DefaultCodingConvention());
    // Enable goog.scope pass
    options.setAliasTransformationHandler(
        AliasTransformationHandler.DO_NOTHING);
    List<SourceFile> inputs = Lists.newArrayList(
        SourceFile.fromCode("testcode", js));
    compiler.compile(
        Lists.<SourceFile>newArrayList(),
        inputs,
        options);
    return compiler.getResult();
  }

  @Test(timeout = 4000)
  public void testSimpleAlias() {
    String js = "var dom = goog.dom;";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertTrue("Compilation should succeed", result.success);
    assertEquals("No errors expected", 0, result.errors.length);
    // Verify the code was transformed? Not easily checkable via Result.
  }

  @Test(timeout = 4000)
  public void testAliasTransitive() {
    String js = "var g = goog; var d = g.dom; d.createElement('DIV');";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertTrue("Compilation should succeed", result.success);
    assertEquals("No errors expected", 0, result.errors.length);
  }

  @Test(timeout = 4000)
  public void testAliasCycle() {
    String js = "var a = b; var b = a;";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertFalse("Compilation should fail due to cycle", result.success);
    assertEquals("One error expected (cycle)", 1, result.errors.length);
    assertTrue("Error should be cycle",
        result.errors[0].getType().equals(ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE));
  }

  @Test(timeout = 4000)
  public void testAliasRedefinition() {
    String js = "var a = x; var a = y;";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertFalse("Compilation should fail due to redefinition", result.success);
    assertEquals("One error expected", 1, result.errors.length);
    assertTrue("Error should be redefinition",
        result.errors[0].getType().equals(ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED));
  }

  @Test(timeout = 4000)
  public void testGoogScopeUsedImproperly() {
    // goog.scope must be alone in a statement
    String js = "var x = goog.scope(function(){});";
    Result result = compileAndGetResult(js);
    assertFalse("Compilation should fail", result.success);
    assertEquals("One error expected", 1, result.errors.length);
    assertTrue("Error should be GOOG_SCOPE_USED_IMPROPERLY",
        result.errors[0].getType().equals(ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY));
  }

  @Test(timeout = 4000)
  public void testGoogScopeBadParameters_extraParam() {
    String js = "goog.scope(function(){}, 1);";
    Result result = compileAndGetResult(js);
    assertFalse("Compilation should fail", result.success);
    assertEquals("One error expected", 1, result.errors.length);
    assertTrue("Error should be GOOG_SCOPE_HAS_BAD_PARAMETERS",
        result.errors[0].getType().equals(ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS));
  }

  @Test(timeout = 4000)
  public void testGoogScopeBadParameters_unnamedFunction() {
    String js = "goog.scope(function fn(){});";
    Result result = compileAndGetResult(js);
    assertFalse("Compilation should fail", result.success);
    assertEquals("One error expected", 1, result.errors.length);
    assertTrue("Error should be GOOG_SCOPE_HAS_BAD_PARAMETERS",
        result.errors[0].getType().equals(ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS));
  }

  @Test(timeout = 4000)
  public void testGoogScopeReferencesThis() {
    String js = "var a = this;";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertFalse("Compilation should fail", result.success);
    assertEquals("One error expected", 1, result.errors.length);
    assertTrue("Error should be GOOG_SCOPE_REFERENCES_THIS",
        result.errors[0].getType().equals(ScopedAliases.GOOG_SCOPE_REFERENCES_THIS));
  }

  @Test(timeout = 4000)
  public void testGoogScopeUsesReturn() {
    String js = "return;";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertFalse("Compilation should fail", result.success);
    assertEquals("One error expected", 1, result.errors.length);
    assertTrue("Error should be GOOG_SCOPE_USES_RETURN",
        result.errors[0].getType().equals(ScopedAliases.GOOG_SCOPE_USES_RETURN));
  }

  @Test(timeout = 4000)
  public void testGoogScopeUsesThrow() {
    String js = "throw new Error();";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertFalse("Compilation should fail", result.success);
    assertEquals("One error expected", 1, result.errors.length);
    assertTrue("Error should be GOOG_SCOPE_USES_THROW",
        result.errors[0].getType().equals(ScopedAliases.GOOG_SCOPE_USES_THROW));
  }

  // ==================================================================
  // Known Defects: testIssue1103a, testIssue1103b, testIssue1103c
  // Expected no error (GOOG_SCOPE_NON_ALIAS_LOCAL) for non-alias locals.
  // The bug is that the compiler reports this error where it should not.
  // ==================================================================

  @Test(timeout = 4000)
  public void testIssue1103a() {
    // Local variable 'a' is not an alias but assigned a non-qualified expression.
    // The original code would report GOOG_SCOPE_NON_ALIAS_LOCAL.
    // Fixed version should allow it.
    String js = "var a = 5;";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertTrue("Compilation should succeed without error for non-alias local",
        result.success);
    assertEquals("No errors expected", 0, result.errors.length);
  }

  @Test(timeout = 4000)
  public void testIssue1103b() {
    // More complex case: local variable 'a' used inside the scope block.
    // The original code caused an internal compiler error.
    String js = "var a = 5; a = a + 1;";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertTrue("Compilation should succeed without internal error",
        result.success);
    assertEquals("No errors expected", 0, result.errors.length);
  }

  @Test(timeout = 4000)
  public void testIssue1103c() {
    // Variable with initial value that is not a qualified name, but later used.
    String js = "var a = 10; use(a);";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertTrue("Compilation should succeed", result.success);
    assertEquals("No errors expected", 0, result.errors.length);
  }

  // ==================================================================
  // Additional tests for type nodes (AliasedTypeNode)
  // ==================================================================

  @Test(timeout = 4000)
  public void testAliasedTypeNode() {
    // In goog.scope: var A = foo.Bar; /** @type {A} */ var x;
    // The type node should be expanded.
    String js = "/** @fileoverview */\n"
        + "goog.scope(function() {\n"
        + "var A = foo.Bar;\n"
        + "/** @type {A} */ var x;\n"
        + "});\n";
    Result result = compileAndGetResult(js);
    assertTrue("Compilation should succeed", result.success);
    assertEquals("No errors expected", 0, result.errors.length);
  }

  @Test(timeout = 4000)
  public void testDeeplyQualifiedTypeAlias() {
    String js = "var A = foo.bar.baz;"
        + "/** @type {A} */ var x;";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertTrue("Compilation should succeed", result.success);
    assertEquals("No errors expected", 0, result.errors.length);
  }

  // ==================================================================
  // Tests for scope shadow and renaming
  // ==================================================================

  @Test(timeout = 4000)
  public void testNamespaceShadow() {
    // If an inner scope shadows a namespace root used in aliases,
    // the renaming mechanism should avoid collisions.
    String js = "var goog = fake; var a = goog.dom;";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertTrue("Compilation should succeed despite shadow", result.success);
    // The shadow variable 'goog' will be renamed, no error expected.
    assertEquals("No errors expected", 0, result.errors.length);
  }

  @Test(timeout = 4000)
  public void testMultipleScopes() {
    // Multiple goog.scope blocks should be handled independently.
    String js = "var a = x; var b = y;";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX
        + "\n" + GOOG_SCOPE_CODE_PREFIX + "var c = z;" + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertTrue("Compilation should succeed", result.success);
    assertEquals("No errors expected", 0, result.errors.length);
  }

  @Test(timeout = 4000)
  public void testAliasInNestedFunctionInsideScope() {
    // Aliases should only be recognized at the top level of goog.scope.
    // Nested function's variables are not aliases.
    String js = "var a = x; (function(){ var b = y; })();";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertTrue("Compilation should succeed", result.success);
    assertEquals("No errors expected", 0, result.errors.length);
  }

  @Test(timeout = 4000)
  public void testNonAliasLocalInsideNestedFunction() {
    // Bug: non-alias local inside nested function should not produce error.
    String js = "(function() { var a = 5; })();";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertTrue("Compilation should succeed", result.success);
    assertEquals("No errors expected", 0, result.errors.length);
  }

  @Test(timeout = 4000)
  public void testEmptyScope() {
    String js = "";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertTrue("Compilation should succeed", result.success);
    assertEquals("No errors expected", 0, result.errors.length);
  }

  @Test(timeout = 4000)
  public void testGoogScopeWithLetOrConst() {
    // With the fix, variables declared with let/const should not trigger error.
    String js = "let a = 5;";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertTrue("Compilation should succeed", result.success);
    assertEquals("No errors expected", 0, result.errors.length);
  }

  @Test(timeout = 4000)
  public void testGoogScopeWithVarAndLaterAlias() {
    // Mix of non-alias and alias variables.
    String js = "var a = 1; var b = goog.array;";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertTrue("Compilation should succeed", result.success);
    assertEquals("No errors expected", 0, result.errors.length);
  }

  // ==================================================================
  // Edge Cases: null preprocessor symbol table, hotSwapScript, etc.
  // ==================================================================

  @Test(timeout = 4000)
  public void testNullPreprocessorSymbolTable() {
    // Create ScopedAliases with null preprocessorSymbolTable.
    // Should not throw NullPointerException.
    Compiler compiler = getCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(
        compiler,
        null, // preprocessorSymbolTable
        AliasTransformationHandler.DO_NOTHING);
    // Provide minimal AST: just an empty script.
    Node root = new Node(Token.SCRIPT);
    Node externs = new Node(Token.SCRIPT);
    try {
      scopedAliases.process(externs, root);
    } catch (Exception e) {
      fail("process should not throw exception with null preprocessorSymbolTable: " + e.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testHotSwapScript() {
    // Test hotSwapScript independently.
    Compiler compiler = getCompiler();
    ScopedAliases scopedAliases = new ScopedAliases(
        compiler,
        null,
        AliasTransformationHandler.DO_NOTHING);
    Node root = new Node(Token.SCRIPT);
    Node originalRoot = null;
    // Should not throw.
    scopedAliases.hotSwapScript(root, originalRoot);
  }

  @Test(timeout = 4000)
  public void testHotSwapScriptWithAliases() {
    // Integrate hotSwapScript with actual code.
    String js = "var a = goog.array; a.sort();";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Compiler compiler = getCompiler();
    CompilerOptions options = new CompilerOptions();
    options.setAliasTransformationHandler(
        AliasTransformationHandler.DO_NOTHING);
    compiler.compile(
        Lists.<SourceFile>newArrayList(),
        Lists.newArrayList(SourceFile.fromCode("testcode", fullJs)),
        options);
    // After compile, get the AST and run hotSwapScript.
    Node root = compiler.getRoot();
    // Ensure 'process' was already called; we can run hotSwapScript again.
    ScopedAliases scopedAliases = new ScopedAliases(
        compiler, null, AliasTransformationHandler.DO_NOTHING);
    try {
      scopedAliases.hotSwapScript(root, null);
    } catch (Exception e) {
      fail("hotSwapScript should not throw: " + e.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testMultipleAliasesNoCycle() {
    String js = "var a = x; var b = y; var c = z;";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertTrue("Compilation should succeed", result.success);
    assertEquals("No errors expected", 0, result.errors.length);
  }

  @Test(timeout = 4000)
  public void testAliasUsageAfterDefine() {
    String js = "var a = goog.array; a.push(1);";
    String fullJs = GOOG_SCOPE_CODE_PREFIX + js + GOOG_SCOPE_CODE_SUFFIX;
    Result result = compileAndGetResult(fullJs);
    assertTrue("Compilation should succeed", result.success);
    assertEquals("No errors expected", 0, result.errors.length);
  }
}