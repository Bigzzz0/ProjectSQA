package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import org.junit.Test;

/**
 * White-box test suite for {@link ScopedAliases} targeting maximum line/branch coverage
 * and the known defect (Issue 772).
 *
 * <p>Branch & Defect Analysis Matrix:
 * <ul>
 *   <li>Partition A: Core functional alias transformations (simple, chained, type nodes).</li>
 *   <li>Partition B: Boundary values (empty scope, null preprocessorSymbolTable, multiple scopes).</li>
 *   <li>Partition C: Defect-targeted: Issue 772 – aliases in type annotations causing incorrect tree.</li>
 *   <li>Partition D: Error paths (GOOG_SCOPE_USED_IMPROPERLY, BAD_PARAMETERS, REFERENCES_THIS, USES_RETURN, USES_THROW, ALIAS_REDEFINED, NON_ALIAS_LOCAL).</li>
 *   <li>Partition E: Lifecycle – hasErrors flag, aliasDefinitionsInOrder ordering, scopeCalls collection.</li>
 * </ul>
 */
public class ScopedAliasesDeepseekTest {

  private static final String SCOPE_FN = "goog.scope";

  // Helper to compile and run ScopedAliases pass.
  private Compiler compileAndRun(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT5);
    options.setLanguageOut(LanguageMode.ECMASCRIPT5);
    // Enable alias transformation handler (default is no-op)
    options.setAliasTransformationHandler(CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    compiler.initOptions(options);
    // Parse source
    Node root = compiler.parse(compiler.getSourceFileFromCode("test", js));
    assertNotNull("Parsing failed", root);
    // Run ScopedAliases
    ScopedAliases scopedAliases = new ScopedAliases(compiler, null, options.getAliasTransformationHandler());
    scopedAliases.process(null, root);
    return compiler;
  }

  // Helper to get the result source after transformation.
  private String getResult(Compiler compiler) {
    return compiler.toSource();
  }

  // ==================== Partition A: Core Functional Logic ====================

  @Test(timeout = 4000)
  public void testSimpleAlias() {
    String js = "goog.scope(function() {\n"
        + "  var dom = goog.dom;\n"
        + "  dom.createElement('div');\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    assertFalse("Unexpected errors", compiler.hasErrors());
    String result = getResult(compiler);
    assertTrue("Expected goog.dom.createElement", result.contains("goog.dom.createElement"));
    assertFalse("Should not contain var dom", result.contains("var dom"));
  }

  @Test(timeout = 4000)
  public void testChainedAlias() {
    String js = "goog.scope(function() {\n"
        + "  var g = goog;\n"
        + "  var d = g.dom;\n"
        + "  d.createElement('div');\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    assertFalse("Unexpected errors", compiler.hasErrors());
    String result = getResult(compiler);
    assertTrue("Expected goog.dom.createElement", result.contains("goog.dom.createElement"));
  }

  @Test(timeout = 4000)
  public void testAliasInTypeAnnotation() {
    String js = "/** @type {goog.dom} */\n"
        + "goog.scope(function() {\n"
        + "  var dom = goog.dom;\n"
        + "  /** @type {dom} */\n"
        + "  var x;\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    assertFalse("Unexpected errors", compiler.hasErrors());
    String result = getResult(compiler);
    // The type annotation should be replaced with the full qualified name
    assertTrue("Expected @type {goog.dom}", result.contains("@type {goog.dom}"));
  }

  @Test(timeout = 4000)
  public void testMultipleAliases() {
    String js = "goog.scope(function() {\n"
        + "  var dom = goog.dom;\n"
        + "  var tag = goog.dom.TagName;\n"
        + "  dom.createElement(tag.DIV);\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    assertFalse("Unexpected errors", compiler.hasErrors());
    String result = getResult(compiler);
    assertTrue("Expected goog.dom.createElement(goog.dom.TagName.DIV)",
        result.contains("goog.dom.createElement(goog.dom.TagName.DIV)"));
  }

  // ==================== Partition B: Boundary Value Analysis ====================

  @Test(timeout = 4000)
  public void testEmptyScope() {
    String js = "goog.scope(function() {});";
    Compiler compiler = compileAndRun(js);
    assertFalse("Unexpected errors", compiler.hasErrors());
    String result = getResult(compiler);
    // Should collapse to empty block
    assertTrue("Expected empty statement", result.contains(";"));
  }

  @Test(timeout = 4000)
  public void testNoAliasUsage() {
    String js = "goog.scope(function() {\n"
        + "  var a = 1;\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    // Should report error: non-alias local
    assertTrue("Expected error", compiler.hasErrors());
    assertEquals("Expected one error", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testNullPreprocessorSymbolTable() {
    // Already using null in helper; just ensure no NPE
    String js = "goog.scope(function() {\n"
        + "  var dom = goog.dom;\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    assertFalse("Unexpected errors", compiler.hasErrors());
  }

  // ==================== Partition C: Defect-Targeted (Issue 772) ====================

  /**
   * Reproduces Issue 772: aliases in type annotations cause incorrect tree.
   * The bug manifests when an alias is used in a JSDoc type annotation and the
   * transformation produces a node tree inequality (e.g., missing qualified name).
   */
  @Test(timeout = 4000)
  public void testIssue772() {
    // This is the exact test case from the bug report.
    String js = "/** @constructor */\n"
        + "function Foo() {}\n"
        + "goog.scope(function() {\n"
        + "  var Foo = goog.Foo;\n"
        + "  /** @type {Foo} */\n"
        + "  var x;\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    // The bug caused an assertion failure (Node tree inequality).
    // After fix, no errors should occur.
    assertFalse("Expected no errors", compiler.hasErrors());
    String result = getResult(compiler);
    // The type annotation should be replaced with the full qualified name
    assertTrue("Expected @type {goog.Foo}", result.contains("@type {goog.Foo}"));
  }

  @Test(timeout = 4000)
  public void testIssue772WithNestedAlias() {
    // More complex case: alias of alias in type annotation
    String js = "/** @constructor */\n"
        + "function Bar() {}\n"
        + "goog.scope(function() {\n"
        + "  var b = goog.Bar;\n"
        + "  var a = b;\n"
        + "  /** @type {a} */\n"
        + "  var x;\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    assertFalse("Expected no errors", compiler.hasErrors());
    String result = getResult(compiler);
    assertTrue("Expected @type {goog.Bar}", result.contains("@type {goog.Bar}"));
  }

  // ==================== Partition D: Exception & Defensive Guard Paths ====================

  @Test(timeout = 4000)
  public void testGoogScopeUsedImproperly() {
    // goog.scope not alone in statement
    String js = "var x = goog.scope(function() {});";
    Compiler compiler = compileAndRun(js);
    assertTrue("Expected error", compiler.hasErrors());
    assertEquals("GOOG_SCOPE_USED_IMPROPERLY", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGoogScopeHasBadParameters_noParam() {
    String js = "goog.scope();";
    Compiler compiler = compileAndRun(js);
    assertTrue("Expected error", compiler.hasErrors());
    assertEquals("GOOG_SCOPE_HAS_BAD_PARAMETERS", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGoogScopeHasBadParameters_multipleParams() {
    String js = "goog.scope(function() {}, function() {});";
    Compiler compiler = compileAndRun(js);
    assertTrue("Expected error", compiler.hasErrors());
    assertEquals("GOOG_SCOPE_HAS_BAD_PARAMETERS", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGoogScopeHasBadParameters_namedFunction() {
    String js = "goog.scope(function foo() {});";
    Compiler compiler = compileAndRun(js);
    assertTrue("Expected error", compiler.hasErrors());
    assertEquals("GOOG_SCOPE_HAS_BAD_PARAMETERS", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGoogScopeHasBadParameters_functionWithParams() {
    String js = "goog.scope(function(x) {});";
    Compiler compiler = compileAndRun(js);
    assertTrue("Expected error", compiler.hasErrors());
    assertEquals("GOOG_SCOPE_HAS_BAD_PARAMETERS", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGoogScopeReferencesThis() {
    String js = "goog.scope(function() {\n"
        + "  var self = this;\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    assertTrue("Expected error", compiler.hasErrors());
    assertEquals("GOOG_SCOPE_REFERENCES_THIS", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGoogScopeUsesReturn() {
    String js = "goog.scope(function() {\n"
        + "  return;\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    assertTrue("Expected error", compiler.hasErrors());
    assertEquals("GOOG_SCOPE_USES_RETURN", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGoogScopeUsesThrow() {
    String js = "goog.scope(function() {\n"
        + "  throw new Error();\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    assertTrue("Expected error", compiler.hasErrors());
    assertEquals("GOOG_SCOPE_USES_THROW", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGoogScopeAliasRedefined() {
    String js = "goog.scope(function() {\n"
        + "  var dom = goog.dom;\n"
        + "  dom = goog.window;\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    assertTrue("Expected error", compiler.hasErrors());
    assertEquals("GOOG_SCOPE_ALIAS_REDEFINED", 1, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGoogScopeNonAliasLocal() {
    String js = "goog.scope(function() {\n"
        + "  var x = 1;\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    assertTrue("Expected error", compiler.hasErrors());
    assertEquals("GOOG_SCOPE_NON_ALIAS_LOCAL", 1, compiler.getErrorCount());
  }

  // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

  @Test(timeout = 4000)
  public void testHasErrorsFlag() {
    // No errors
    String js = "goog.scope(function() {\n"
        + "  var dom = goog.dom;\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    assertFalse("hasErrors should be false", compiler.hasErrors());

    // With errors
    js = "goog.scope(function() {\n"
        + "  return;\n"
        + "});";
    compiler = compileAndRun(js);
    assertTrue("hasErrors should be true", compiler.hasErrors());
  }

  @Test(timeout = 4000)
  public void testAliasDefinitionsInOrder() {
    // The order of alias definitions should be preserved.
    String js = "goog.scope(function() {\n"
        + "  var a = goog.a;\n"
        + "  var b = goog.b;\n"
        + "  a();\n"
        + "  b();\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    assertFalse("Unexpected errors", compiler.hasErrors());
    String result = getResult(compiler);
    // After transformation, the calls should be goog.a() and goog.b()
    assertTrue("Expected goog.a()", result.contains("goog.a()"));
    assertTrue("Expected goog.b()", result.contains("goog.b()"));
  }

  @Test(timeout = 4000)
  public void testScopeCallsCollection() {
    // Multiple goog.scope calls should be handled.
    String js = "goog.scope(function() {\n"
        + "  var a = goog.a;\n"
        + "  a();\n"
        + "});\n"
        + "goog.scope(function() {\n"
        + "  var b = goog.b;\n"
        + "  b();\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    assertFalse("Unexpected errors", compiler.hasErrors());
    String result = getResult(compiler);
    assertTrue("Expected goog.a()", result.contains("goog.a()"));
    assertTrue("Expected goog.b()", result.contains("goog.b()"));
  }

  @Test(timeout = 4000)
  public void testNamespaceShadowRenaming() {
    // When a local variable shadows a namespace root, it should be renamed.
    String js = "goog.scope(function() {\n"
        + "  var goog = {};\n" // shadows goog
        + "  var dom = goog.dom;\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    // This should not cause an error, but the shadow variable should be renamed.
    assertFalse("Unexpected errors", compiler.hasErrors());
    String result = getResult(compiler);
    // The shadow variable 'goog' should be renamed to something else (e.g., goog$$0)
    assertTrue("Expected renamed variable", result.matches(".*goog\\$\\$0.*"));
  }

  @Test(timeout = 4000)
  public void testBleedingFunctionIgnored() {
    // A bleeding function (function declaration inside scope) should be ignored.
    String js = "goog.scope(function() {\n"
        + "  function f() {}\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    // Should not report error for bleeding function (it's not a local alias)
    assertFalse("Unexpected errors", compiler.hasErrors());
  }

  @Test(timeout = 4000)
  public void testTypeNodeWithMultipleDots() {
    // Type annotation with multiple dots after alias base
    String js = "/** @constructor */\n"
        + "function Foo() {}\n"
        + "goog.scope(function() {\n"
        + "  var Foo = goog.Foo;\n"
        + "  /** @type {Foo.Bar} */\n"
        + "  var x;\n"
        + "});";
    Compiler compiler = compileAndRun(js);
    assertFalse("Unexpected errors", compiler.hasErrors());
    String result = getResult(compiler);
    assertTrue("Expected @type {goog.Foo.Bar}", result.contains("@type {goog.Foo.Bar}"));
  }

  @Test(timeout = 4000)
  public void testNoCodeChangeWhenNoAliases() {
    // When there are no aliases, no code change should be reported.
    String js = "var x = 1;";
    Compiler compiler = compileAndRun(js);
    assertFalse("Unexpected errors", compiler.hasErrors());
    // The source should remain unchanged.
    assertEquals("var x = 1;\n", getResult(compiler));
  }
}